# Minecraft Function Language (In development)
Minecraft Function Lang, a basic scripting language for minecraft
  
This project isn't intended to be used anywhere, just a hobby project to create
a scripting language with more functionality than the `.mcfunction` format,
which is literally just a command list with comment support
  
Note: The parser uses '$' to differentiate between regular command lines
and statements/expressions

## TODO
1. [X] Lexer
2. [X] Parser
3. [x] If statements
4. [x] Functions
5. [ ] Loops (`$for` and `$while`)
6. [ ] Operators
    1. [ ] add (`+`)
    2. [ ] sub (`-`)
    3. [ ] div (`/`)
    4. [ ] mul (`*`)
    5. [ ] pow (`x**y`)
    6. [ ] or (`|`) 
    7. [ ] and (`&`) 
    8. [ ] xor (`^`) 
    9. [ ] negate (`!`) 
    10. [ ] bitwise negate (`~`)
7. [ ] String templating (`$bindingIdentifier` and `${expression}`) for both commands and quoted strings
8. [ ] Accessing properties on objects and calling methods
9. [ ] PaperMC support (Currently just a parser and interpreter)

## Syntax Example
```txt
# This is a comment
tellraw @a "Hello, world!"

/* This is also a comment. These 2 lines below will call the methods */
$a_function()
$another_function()

$function a_function() {
  $print("I've been called!")
  $return true
}

$function another_function() {
  $print("I've also been called!")
}
```

## Built in values and functions
### `print(message...)`
Prints an array of messages that are joined together to the console
  
Example:
```txt
$print("Hello, world!")
```

### `systemTimeMillis`
Numeric value for the system time in milliseconds, wrapper for Java's 
`System.currentTimeMillis()`
  
Example: 
```txt
# Gets the system time at this moment
$time = systemTimeMillis
```

### `scriptTime`
Seconds since the script was activated, floating point number.

Example:
```txt
# Gets the script time at this moment
$time = scriptTime
```

## Using through Java
```java
import me.jules.mcfl.*;
import me.jules.mcfl.interpreter.*;
import me.jules.mcfl.ast.*;

// ...

Path filePath = /* Get the path of the file you want to load */;

// Parse file
FunctionFile file = FunctionLang.parse(filePath);

// Create file scope and execution context
Scope scope = FunctionLang.defineStandardScope();
ExecContext ctx = new ExecContext();

// This will take a string command as input and 'execute' it
ctx.setExecutor(command -> {
  System.out.println("command=" + command);
  return 0;
});

// Evaluate file
try {
  file.execute(ctx, scope);
} catch (EvaluationError err) {
  err.printStackTrace();
}
```