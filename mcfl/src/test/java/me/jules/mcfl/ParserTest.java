package me.jules.mcfl;

import me.jules.mcfl.interpreter.BindingObject;
import me.jules.mcfl.interpreter.Bindings;
import me.jules.mcfl.interpreter.ExecContext;
import me.jules.mcfl.interpreter.ReturnValue;
import me.jules.mcfl.interpreter.Scope;
import me.jules.mcfl.interpreter.ValueRef;
import org.junit.jupiter.api.Test;

public class ParserTest {

  @Test
  void generalTest() {
    TestUtils.evaluateResource("general-test.mcfl");
  }

  @Test
  void unaryTest() {
    TestUtils.evaluateResource("unary-expr-test.mcfl");
  }

  @Test
  void binaryTest() {
    TestUtils.evaluateResource("binary-expr-test.mcfl");
  }

  @Test
  void comparisonTest() {
    TestUtils.evaluateResource("comparison-test.mcfl");
  }

  @Test
  void forTest() {
    TestUtils.evaluateResource("loop-test.mcfl");
  }

  @Test
  void testCommandTemplating() {
    TestUtils.evaluateResource("command-template-test.mcfl");
  }

  @Test
  void operatorTest() {
    TestUtils.evaluateResource("operators-test.mcfl");
  }

  @Test
  void mcFunctionTest() {
    TestUtils.evaluateResource("player_run.mcfunction");
  }

  @Test
  void stringConcatTest() {
    TestUtils.evaluateResource("string-concat.mcfl");
  }

  @Test
  void fizzBuzzTest() {
    String script = """
    #FizzBuzz
        
    $let output = ""
        
    $for ($let i = 1; i <= 100; i++) {
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
    """;

    TestUtils.evaluateString(script, null);
  }

  @Test
  void propertyAccessTest() {
    TestUtils.evaluateResource("object-access-test.mcfl", scope -> {

      BindingObject object = new BindingObject();
      object.defineValue("x", 10, 0);
      object.defineValue("y", 45, 0);

      object.defineValue(
          "func",
          new ScriptCallable() {
            @Override
            public ReturnValue call(ExecContext ctx, Scope scope, ValueRef[] params) {
              System.out.println("func called");
              return ReturnValue.directWrap(object);
            }
          },
          0
      );

      scope.defineValue("obj", object, Bindings.FLAG_CONST);
    });
  }
}
