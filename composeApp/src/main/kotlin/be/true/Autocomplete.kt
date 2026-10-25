package be.`true`

object Autocomplete {
    val KEYWORDS = listOf(
        ":math", ":math:sum", ":math:min", ":math:max", ":math:avg", ":math:product",
        ":math:count", ":math:sort", ":math:popcount", ":math:clz", ":math:ctz",
        ":data:substring", ":data:indexOf", ":data:replace", ":data:lower",
        ":data:upper", ":data:trim", ":data:chars", ":data:charAt", ":data:length",
        ":data:reverse", ":data:unique", ":data:contains", ":data:slice",
        ":data:push", ":data:pop", ":data:insert", ":data:remove", ":data:swap",
        ":data:set", ":data:deleteRange", ":data:replaceRange", ":data:sort",
        ":data:join", ":data:length", ":filter", ":matches",
        ":json:get", ":json:set", ":json:names", ":json:values",
        ":console", ":run", ":file:create", ":file:read", ":file:update",
        ":file:delete", ":file:list", ":file:exists", ":file:data", ":file:origin", ":file:lines",
        ":api:get", ":api:post", ":api:put", ":api:delete", ":api:patch", ":api:websocket",
        ":save", ":load", ":get", ":post", ":size",
        "activeTab", "return", "pause", "resume", "stop", "reset", "next", "back",
        ":source:function", "agent:wait", "-> nothing"
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