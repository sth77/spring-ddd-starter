---
message: |
  hygen {bold field add} --name [NAME] --type [TYPE] --aggregate [AGGREGATE] --feature [FEATURE] [OPTIONS]

  {bold Field types:}
    string          Required String field (@NotBlank, VARCHAR)
    nullable-string Optional String field (@Nullable, VARCHAR)
    enum            Enum field (requires --values A,B,C)
    association     Cross-aggregate reference (requires --target, --targetModule)
    boolean         Boolean field (use --default false/true)
    instant         Instant timestamp (TIMESTAMP WITH TIME ZONE)
    text            Unbounded nullable text (TEXT column)

  {bold Options:}
    --required       NOT NULL in DB, @NotBlank/@NotNull in command
    --maxLength N    VARCHAR(N), @Size(max=N) in command
    --default VALUE  DEFAULT in migration
    --values A,B,C   Enum values (required for enum type)
    --target Name    Target aggregate (required for association)
    --targetModule m Target module (required for association)
    --migration V006 Standalone migration file (default: append to V0001)

  {bold Examples:}
    hygen field add --name title --type string --aggregate Sample --feature sample --maxLength 500
    hygen field add --name composer --type nullable-string --aggregate Sample --feature sample
    hygen field add --name source --type enum --aggregate Sample --feature sample --values CATALOG,MANUAL,IMPORT
    hygen field add --name person --type association --aggregate Sample --feature sample --target Person --targetModule person
    hygen field add --name archived --type boolean --aggregate Sample --feature sample --default false
    hygen field add --name lastInteraction --type instant --aggregate Sample --feature sample --required
    hygen field add --name notes --type text --aggregate Sample --feature sample
---
