package be.simple

/**
 * Autocomplete — словарь ключевых слов для UI-подсказок в редакторе.
 * Дополняется из Extensions.list() динамически.
 */
object Autocomplete {
    private val CORE = listOf(
        ":math", ":math:sum", ":math:min", ":math:max", ":math:avg",
        ":math:product", ":math:count", ":math:sort",
        ":sum", ":min", ":max", ":avg", ":product", ":count", ":sort",

        ":data:size", ":data:empty", ":data:substring", ":data:indexOf",
        ":data:replace", ":data:lower", ":data:upper", ":data:trim",
        ":data:chars", ":data:charAt", ":data:reverse", ":data:unique",
        ":data:contains", ":data:slice", ":data:sort", ":data:join",
        ":data:push", ":data:pop", ":data:remove", ":data:swap",
        ":data:deleteRange", ":data:replaceRange",
        ":data:groupby",
        ":push", ":pop", ":peek", ":remove", ":swap",
        ":filter", ":matches", ":map", ":reduce", ":fold", ":each",
        ":range", ":repeat", ":size", ":s",

        ":json:get", ":json:set", ":json:names", ":json:values",
        ":xml:get", ":xml:names", ":xml:values",

        ":api:get", ":api:post", ":api:put", ":api:delete", ":api:patch",
        ":api:websocket",
        ":api:get:retry", ":api:post:retry", ":api:put:retry",
        ":api:delete:retry", ":api:patch:retry",
        ":api:mock", ":api:mock:reset", ":api:mock:list",
        ":get", ":post", ":save", ":load",

        ":file:create", ":file:read", ":file:update", ":file:delete",
        ":file:list", ":file:exists", ":file:data", ":file:origin", ":file:lines",
        ":file:stream", ":file:lines:stream",

        ":stream:next", ":stream:close", ":stream:reset", ":stream:limit", ":stream:list",
        ":stream:seek",

        ":terminal", ":terminal:write", ":terminal:read", ":terminal:close", ":terminal:list",

        ":open:url", ":open:file", ":open:folder",

        ":secret:get", ":secret:set", ":secret:remove", ":secret:list",

        ":send:telegram", ":send:max", ":send:email",

        ":schedule", ":schedule:list", ":schedule:stop", ":schedule:limit",

        ":sound:play", ":sound:stop",

        ":websocket:listen", ":websocket:send", ":websocket:message",
        ":websocket:clients", ":websocket:close",

        ":mem:alloc", ":mem:free", ":mem:write", ":mem:read",
        ":mem:copy", ":mem:move", ":mem:fill", ":mem:size", ":mem:dump", ":mem:reset",

        ":background", ":background:reset",

        ":ask",

        ":agent:broadcast", ":agent:send", ":agent:inbox", ":agent:inbox:clear",
        ":agent:status", ":agent:wait", ":agent:wait:timeout",

        ":simple:click", ":simple:clickAt", ":simple:type",
        ":simple:openTerminal", ":simple:sendTerminal",
        ":simple:typeTerminal", ":simple:closeTerminal",
        ":simple:search", ":simple:search:next", ":simple:search:prev", ":simple:search:clear",

        ":error", ":error:line", ":error:message", ":error:lines", ":error:messages",
        ":error:resolve", ":error:pause", ":assert:pause", ":assert",

        ":run", ":log", ":message", ":result", ":activeTab",
        ":pauseAll", ":resumeAll",

        ":pinned:add", ":pinned:remove", ":pinned:list", ":pinned:clear",

        ":watch:add", ":watch:remove", ":watch:list",
        ":diff", ":history",
        ":agent:sync",

        ":ext:list"
    )

    fun all(): List<String> {
        val dynamic = Extensions.list().map { ":ext:$it" }
        return (CORE + dynamic).distinct().sorted()
    }

    fun startsWith(prefix: String): List<String> {
        if (prefix.isEmpty()) return all()
        return all().filter { it.startsWith(prefix, ignoreCase = true) }
    }
}