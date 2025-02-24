/**
 * A Link Object represents a hyperlink from the containing resource to
 * a URI. 
 * 
 * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-5}
 */
export interface Link {

    /**
     * The "href" property is REQUIRED.
     * 
     * Its value is either a URI {@link https://datatracker.ietf.org/doc/html/rfc3986|RFC3986} 
     * or a URI Template {@link https://datatracker.ietf.org/doc/html/rfc6570|RFC6570}.
     * 
     * If the value is a URI Template then the Link Object SHOULD have a "templated" attribute whose value is true.
     * 
     * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-5.1}
     */
    href: string;
    
    /**
     * The "templated" property is OPTIONAL.
     * 
     * Its value is boolean and SHOULD be true when the Link Object's "href"
     * property is a URI Template.
     * 
     * Its value SHOULD be considered false if it is undefined or any other
     * value than true.
     * 
     * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-5.2}
     */
    templated?: boolean;

    /**
     * The "type" property is OPTIONAL.
     * 
     * Its value is a string used as a hint to indicate the media type
     * expected when dereferencing the target resource.
     * 
     * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-5.3}
     */
    type?: string;

    /**
     * The "deprecation" property is OPTIONAL.
     * 
     * Its presence indicates that the link is to be deprecated (i.e.
     * removed) at a future date.  Its value is a URL that SHOULD provide
     * further information about the deprecation.
     * 
     * A client SHOULD provide some notification (for example, by logging a
     * warning message) whenever it traverses over a link that has this
     * property.  The notification SHOULD include the deprecation property's
     * value so that a client maintainer can easily find information about
     * the deprecation.
     * 
     * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-5.4}
     */
    deprecation?: string;

    /**
     * The "name" property is OPTIONAL.
     * 
     * Its value MAY be used as a secondary key for selecting Link Objects
     * which share the same relation type.
     * 
     * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-5.5}
     */
    name?: string;

    /**
     * The "profile" property is OPTIONAL.
     * 
     * Its value is a string which is a URI that hints about the profile (as
     * defined by {@link https://www.rfc-editor.org/info/rfc6906|RFC6906}) 
     * of the target resource.
     * 
     * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-5.6}
     */
    profile?: string;

    /**
     * The "title" property is OPTIONAL.
     * 
     * Its value is a string and is intended for labelling the link with a
     * human-readable identifier (as defined by {@link https://datatracker.ietf.org/doc/html/rfc5988|RFC5988}).
     * 
     * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-5.7}
     */
    title?: string;

    /**
     * The "hreflang" property is OPTIONAL.
     * 
     * Its value is a string and is intended for indicating the language of
     * the target resource (as defined by {@link https://datatracker.ietf.org/doc/html/rfc5988|RFC5988}).
     * 
     * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-5.8}
     */
    hreflang?: string;

}

/**
 * A Resource Object represents a resource.
 * 
 * It has two reserved properties:
 * 
 * (1)  "_links": contains links to other resources.
 * 
 * (2)  "_embedded": contains embedded resources.
 * 
 * All other properties MUST be valid JSON, and represent the current
 * state of the resource.
 * 
 * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-4}
 */
export interface Resource {

    /**
     * The reserved "_links" property is OPTIONAL.
     * 
     * It is an object whose property names are link relation types (as
     * defined by {@link https://datatracker.ietf.org/doc/html/rfc5988|RFC5988} 
     * and values are either a Link Object or an array
     * of Link Objects.  The subject resource of these links is the Resource
     * Object of which the containing "_links" object is a property.
     * 
     * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-4.1.1}
     */
    _links?: {[rel: string]: Link};

    /**
     * The reserved "_embedded" property is OPTIONAL
     * 
     * It is an object whose property names are link relation types (as
     * defined by [RFC5988]) and values are either a Resource Object or an
     * array of Resource Objects.
     * 
     * Embedded Resources MAY be a full, partial, or inconsistent version of
     * the representation served from the target URI.
     * 
     * @see {@link https://datatracker.ietf.org/doc/html/draft-kelly-json-hal-11#section-4.1.2}
     */
    _embedded?: {[rel: string]: Resource | Array<Resource>};

}

export interface CollectionResource<T extends Resource> extends Array<T>, Resource {
    
}