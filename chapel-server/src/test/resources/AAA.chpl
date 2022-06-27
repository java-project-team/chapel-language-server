var declarationGlobal;
declarationGlobal = 5;

var declarationAndDefinitionGlobal = 7;
declarationGlobal += declarationAndDefinitionGlobal;

module Main {
    var localMain = 12;
    localMain -= declarationAndDefinitionGlobal + 9;

    foo(); // wrong
    A.foo();

    module A {
        proc foo() {}
        foo();

        Main.B.foo();
        B.foo(); // wrong

        Main.C.foo(); // wrong
        C.foo(); // wrong
    }

    var declarationLocalMain;

    module B {
        proc foo() {}
        foo();

        Main.A.foo();
        A.foo(); // wrong

        var localB;
        localB = Main.localMain;
    }

    private module C {
        proc foo() {}
        foo();
        Main.B.foo();
    }

    class Class {
        proc foo() {
            another();
        }
        proc another() {

        }
    }

    C.foo(); // wrong

    declarationLocalMain = B.localB;
}