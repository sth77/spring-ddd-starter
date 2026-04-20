// hygen field add --name <fieldName> --type <fieldType> --aggregate <Aggregate> --feature <feature> [options]
//
// Field types:
//   string          — required String field (@NotBlank, trimmed)
//   nullable-string — optional String field (@Nullable)
//   enum            — enum field (--values CATALOG,MANUAL,...)
//   association     — cross-aggregate reference (--target Musician --targetModule identity)
//   boolean         — boolean field (--default false)
//   instant         — Instant timestamp field
//   text            — unbounded nullable text (TEXT column)
//
// Options:
//   --required       — field is NOT NULL in DB, @NotBlank/@NotNull in command
//   --maxLength N    — VARCHAR(N), @Size(max=N) in command
//   --default VALUE  — DEFAULT in migration
//   --values A,B,C   — enum values (required for enum type)
//   --target Name    — target aggregate (required for association type)
//   --targetModule m — target module package (required for association type)
//   --migration V006 — generate standalone migration; omit to append to V0001

module.exports = {
  params: ({ args }) => {
    // Validate required params
    if (!args.name) throw new Error('--name is required')
    if (!args.type) throw new Error('--type is required (string|nullable-string|enum|association|boolean|instant|text)')
    if (!args.aggregate) throw new Error('--aggregate is required')
    if (!args.feature) throw new Error('--feature is required')
    if (args.type === 'enum' && !args.values) throw new Error('--values is required for enum type')
    if (args.type === 'association' && !args.target) throw new Error('--target is required for association type')
    if (args.type === 'association' && !args.targetModule) throw new Error('--targetModule is required for association type')
    return args
  }
}
