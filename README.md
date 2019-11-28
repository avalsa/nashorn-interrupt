# nashorn-interrupt
Nashorn-interrupt is java-agent (only premain currenty implemented so can't attach to running JVM) which do nashorn scripts interruptable. It modifies scripts what passed to `NashornScriptEngine` adding checks of `interrupt` status.
It protects from `while (true) {...}` or `for (i = 0; i >= 0; i++) {...}` problems. For more cases see `RewriteVisitorWithAgentTest`.
# how it works
It modifies AST of script adding invocation of interrupt checker function.
`while (true) {...}` converted to `while (true) { _interrupt_check(); ... }` before script evaluated.
# Usage
1. make agent with `mvn package`
2. add java agent option when run `java -javaagent:agent.jar` 
