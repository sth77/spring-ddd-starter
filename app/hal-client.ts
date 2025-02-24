import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { map, mergeMap, shareReplay } from 'rxjs/operators';
import { ApiResource } from './model/api';
import { CollectionResource, Link, Resource } from './model/hal-types';
import { environment } from '../../env/environment';

const API_PATH = '/api';
const API_CACHE_SIZE = 1;
const SELF_RELATION = 'self';
const QUERY_MARKER = "{?";
const QUERY_PARAMS_REGEX = /{\?(?:(\w+\*?),)*(\w+\*?)}/;

/**
 * Opinionated client for consuming a REST API which serves HAL resources according to the internet draft
 * {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11|draft-kelly-json-hal-11}
 * (media type 'application/hal+json').
 *
 * The client assumes that
 * 1) the REST API reachable under '/api' serves HAL documents
 * 2) the API root document contains links to the aggregates of the application
 * 3) an aggregate collection embeds the array of aggregates in the property _embedded[<rel>],
 *    where <rel> is the relation name of the collection relation. If the array is large, it should
 *    be paged, with links for next and / or previous page.
 * 4) the aggregate collection resource contains links that apply on the collection, such as
 *    creating a new aggregate
 * 5) the aggregates contained in the aggregate collections are HAL resources with links reveiling
 *    the current business operations or 'actions' that a user may execute
 * 6) actions are executed using POST requests
 *
 * The client therefore needs to know the link relations and the data models used for reading data
 * and for executing actions, but not the resource URLs.
 */
@Injectable({
  providedIn: 'root',
})
export class HalClient {
  apiRoot$: Observable<Resource>;

  constructor(private http: HttpClient) {
    const fullApiPath = `${environment.apiUrl}${API_PATH}`;
    this.apiRoot$ = this.http.get<ApiResource>(fullApiPath).pipe(shareReplay(API_CACHE_SIZE));
  }

  public clearCache(): void {
    const fullApiPath = `${environment.apiUrl}${API_PATH}`;
    alert(fullApiPath);
    this.apiRoot$ = this.http.get<ApiResource>(fullApiPath).pipe(shareReplay(API_CACHE_SIZE));
  }

  public getEmbedded<T>(resource: Resource, relation: string): T | undefined {
    const embedded = resource?._embedded;
    return embedded ? <T>embedded[relation] : undefined;
  }

  public getLink(resource: Resource, linkRelation: string): Link | undefined {
    const links = resource?._links;
    return links ? links[linkRelation] : undefined;
  }

  public getLinkUri(
    resource: Resource,
    linkRelation: string,
    params?: Map<string, string | number>,
  ): string | undefined {
    const links = resource?._links;
    const link = links ? links[linkRelation] : undefined;
    return link ? this.resolveParams(link.href, params) : undefined;
  }

  public getSelfUrl(resource: Resource): string | undefined {
    return this.getLink(resource, SELF_RELATION)?.href;
  }

  public load<T>(
    linkRelation: string,
    parent?: Resource,
    params?: Map<string, string | number>,
  ): Observable<T> {
    if (parent) {
      const embeddedResult = this.getEmbedded<T>(parent, linkRelation);
      if (embeddedResult) return of(embeddedResult);
    }
    const parent$ = !parent ? this.apiRoot$ : of(parent);
    return this.doLoad(linkRelation, parent$, params);
  }

  public loadUrl<T>(url: string, params?: Map<string, string | number>): Observable<T> {
    const resolvedUrl = params ? this.resolveParams(url, params) : url;
    return this.http.get<T>(resolvedUrl);
  }

  public loadCollection<T extends CollectionResource<any>>(
    linkRelation: string,
    parent?: Resource,
    params?: Map<string, string | number>,
  ): Observable<T> {
    return this.load<Resource>(linkRelation, parent, params).pipe(
      map((resource) => {
        const result = this.getEmbedded<any>(resource, linkRelation) || [];
        if (!result) {
          throw new Error('Embedded relation "' + linkRelation + '" not found');
        }
        result._links = resource._links;
        result._embedded = resource._embedded;
        if (!Array.isArray(result)) {
          throw new Error('Embedded relation "' + linkRelation + '" is not an array');
        }
        return <T>result;
      }),
    );
  }

  public can<T extends Resource>(action: string, entity: T): boolean {
    return !!this.getLink(entity, action);
  }

  public do<T extends Resource>(
    action: string,
    entity: T,
    parameters?: any,
    responseType?: string,
  ): Observable<any> {
    const url = this.getLink(entity, action)?.href;
    if (!url) {
      return throwError(() => new Error('No action URL found for action ' + action));
    }
    const httpOptions: any = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        Accept: 'text/plain, application/json, application/hal+json',
      }),
      responseType: responseType || 'json',
    };
    if (action === 'cancel') {
      return this.http.delete<T>(url, httpOptions);
    } else {
      return this.http.post<T>(url, JSON.stringify(parameters || {}), httpOptions);
    }
  }

  public onApiError(error: any) {
    const message = error.message || error.error || error || 'Error calling backend';
    alert('API: ' + message);
  }

  private doLoad<T>(
    linkRelation: string,
    parent: Observable<Resource>,
    params?: Map<string, string | number>,
  ): Observable<T> {
    return parent.pipe(
      mergeMap((parent) => {
        const link = this.getLink(parent, linkRelation);
        if (!link) {
          return throwError(
            () => new Error('Link relation "' + linkRelation + '" not found on parent resource'),
          );
        }
        const fullUrl = this.getFullUrl(link.href);
        const url = this.resolveParams(fullUrl, params);
        return this.http.get<T>(url);
      }),
    );
  }

  private getFullUrl(relativeUrl: string): string {
    if (relativeUrl.startsWith('http') || relativeUrl.startsWith('https')) {
      return relativeUrl;
    }
    return `${environment.apiUrl}${relativeUrl}`;
  }

  private resolveParams(uri: string, params?: Map<string, string | number>): string {
    let result = uri;
    result = this.resolveQueryParams(result, params);
    if (params) {
      result = this.resolvePathParams(result, params);
    }
    return result;
  }

  /**
   * Resolves query parameters such as paging parameters or projection parameters
   * in the given URI. For each parameter template encountered, a parameter value
   * is looked up in the passed-in 'params' map. If a value is present, the parameter
   * is appended to the url, otherwise, the parameter is suppressed.
   * @param uri An URI potentially containing parameters
   * @param params a map with parameter values to substitute
   * @private
   */
  private resolveQueryParams(uri: string, params: Map<string, string | number> | undefined): string {
    const parts = QUERY_PARAMS_REGEX.exec(uri);
    if (!parts) {
      return uri;
    }
    const uriStripped = uri.substring(0, uri.indexOf(QUERY_MARKER));
    if (!params) {
      return uriStripped;
    }
    const values: string[] = [];
    parts.slice(1).filter(it => !!it).map((part) => {
      const paramName = part.replace('*', '');
      if (params.has(paramName)) {
        values.push(paramName + '=' + params.get(paramName));
      }
    });
    return uriStripped + '?' + values.join('&');
  }

  private resolvePathParams(uri: string, params: Map<string, string | number>): string {
    let result = uri;
    for (const paramName of params.keys()) {
      result = result.replace('{' + paramName + '}', '' + params.get(paramName));
    }
    return result;
  }
}
