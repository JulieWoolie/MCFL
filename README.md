# Minecraft Function Language (Not affiliated with Mojang, hobby project)
Minecraft Function Lang, a basic scripting language for minecraft
  
This project isn't intended to be used anywhere, just a hobby project to create
a scripting language with more functionality than the `.mcfunction` format,
which is literally just a command list with comment support
  
Note: The parser uses '$' to differentiate between regular command lines
and statements/expressions
  
PS, do not expect this to be an ultra-fast implementation of a scripting language,
this is just a hobby project that works with an interpreter

## TODO
1. [X] Lexer
2. [X] Parser
3. [x] If statements
4. [x] Functions
5. [X] Loops (`$for` and `$while`)
6. [x] Operators
    1. [x] add (`+`)
    2. [x] sub (`-`)
    3. [x] div (`/`)
    4. [x] mul (`*`)
    5. [x] pow (`x**y`)
    6. [x] or (`|`) 
    7. [x] and (`&`) 
    8. [x] xor (`^`) 
    9. [x] negate (`!`) 
    10. [x] bitwise negate (`~`)
7. [X] String templating (`$bindingIdentifier` and `${expression}`) for both commands and quoted strings
8. [X] Accessing properties on objects and calling methods
9. [ ] PaperMC support (Currently just a parser and interpreter)

## Features
Completely backwards compatible with `.mcfunction` files
  
Support for all operators found in hava
```text
$let x = 10
$let y = 20

$print(-x)
$print(+x)
$print(++x)
$print(--x)
$print(x--)
$print(x++)
$print(x + y)
$print(x - y)
$print(x * y)
$print(x / y)
$print(x | y)
$print(x & y)
$print(x % y)
$print(x << y)
$print(x < y)
$print(x > y)
$print(x >= y)
$print(x <= y)
$print(x <<< y)
$print(x >> y)
$print(x >>> y)
```
String and command templating
```text
$const const_value = 12
$const other_const = 13

# Final result will be 'tellraw @a {"text":"25"}' 
tellraw @a {"text":"${const_value + other_const}"}

# Final result will be "const_value+other_const = 25"
$const const_string = "const_value+other_const = ${const_value + other_const}"
```
Run commands alongside statements and expressions
```text
$const MAX_TICKS = 200

$global-let tickCount = 0
$tickCount++

$if (tickCount >= MAX_TICKS) {
  tellraw @a {"text":"Timer has ended, kicking all players out of arena","color":"green","bold":true}
  teleport @a[x=0,y=75,z=0,distance=..20] 51.5 80 51.5
}

schedule("1t")
```
`for`, `while` and `do {} while ()` loops
```text
#FizzBuzz

$let output = ""

$for ($let i = 0; i < 100; i++) {
  $if (i % 3 == 0) {
    $output += "Fizz"
  }
  
  $if (i % 5 == 0) {
    $output += "Buzz"
  }
  
  $if (output == "") {
    $output = i
  }
  
  tellraw @a "${output}"
  $output = ""
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