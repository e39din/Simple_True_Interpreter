package be.`true`

object Autocomplete {
    val KEYWORDS = listOf(
        // Math
        ":math", ":math:sum", ":math:min", ":math:max", ":math:avg", ":math:product",
        ":math:count", ":math:sort", ":math:popcount", ":math:clz", ":math:ctz",
        ":sum", ":min", ":max", ":avg", ":product", ":count",
        // Data — string
        ":data:substring", ":data:indexOf", ":data:replace", ":data:lower",
        ":data:upper", ":data:trim", ":data:chars", ":data:charAt",
        ":data:reverse", ":data:unique", ":data:contains", ":data:slice",
        ":data:join", ":data:sort",
        // Data — list
        ":data:push", ":data:pop", ":data:remove", ":data:swap",
        ":data:deleteRange", ":data:replaceRange",
        ":push", ":pop", ":peek", ":remove", ":swap",
        ":size", ":s", ":reverse", ":unique", ":contains", ":slice", ":join", ":sort",
        ":stack:peek", ":stack:push", ":stack:pop",
        // Functional
        ":map", ":reduce", ":fold", ":each",
        ":filter", ":matches",
        // JSON
        ":json:get", ":json:set", ":json:names", ":json:values",
        // File
        ":file:create", ":file:read", ":file:update", ":file:delete",
        ":file:list", ":file:exists", ":file:data", ":file:origin", ":file:lines",
        ":path",
        // API
        ":api:get", ":api:post", ":api:put", ":api:delete", ":api:patch", ":api:websocket",
        // Console / IO
        ":console", ":save", ":load", ":get", ":post",
        // Error / Assert
        ":error", ":error:line", ":error:message", ":error:lines", ":error:messages",
        ":error:resolve", ":assert",
        // Pause / Resume
        ":pauseAll", ":resumeAll",
        // Agent
        ":agent:status", ":agent:wait", ":agent:wait:timeout",
        // Settings
        ":historyLimit", ":logLimit", ":strictMode", ":autoSync",
        // Simple — base
        ":simple:open", ":simple:save", ":simple:saveAs", ":simple:newCycle",
        ":simple:newAgent", ":simple:openFolder", ":simple:rename", ":simple:delete",
        ":simple:reset", ":simple:close", ":simple:moveUp", ":simple:moveDown",
        ":simple:startAll", ":simple:pauseAll", ":simple:stopAll",
        ":simple:stepAll", ":simple:stepBackAll", ":simple:settings",
        ":simple:about", ":simple:samples", ":simple:activeTab",
        // Simple — UI automation
        ":simple:click", ":simple:clickAt", ":simple:type",
        // Simple — Terminal
        ":simple:openTerminal", ":simple:closeTerminal", ":simple:sendTerminal", ":simple:typeTerminal",
        // Simple — Search
        ":simple:search", ":simple:search:query", ":simple:search:matches",
        ":simple:search:current", ":simple:search:count", ":simple:search:next",
        ":simple:search:prev", ":simple:search:clear",
        // Meta
        ":result", ":parameters", ":message", ":range", ":repeat",
        ":activeTab", ":run", ":log", ":simple",
        // Bare words
        "activeTab", "return", "pause", "resume", "stop", "reset", "next", "back",
        "-> nothing",
        // Literal
        "\\ "
    )

    fun complete(prefix: String, keywords: List<String> = KEYWORDS): String? {
        if (prefix.isBlank()) return null
        return keywords.firstOrNull { it.startsWith(prefix) && it != prefix }
    }

    fun suggestions(prefix: String, keywords: List<String> = KEYWORDS): List<String> {
        if (prefix.isBlank()) return emptyList()
        return keywords.filter { it.startsWith(prefix) && it != prefix }
    }
}