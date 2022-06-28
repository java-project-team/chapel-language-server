var t;

t = 353;
t += 3;

module Main {
    var x = 12;

    module Zun {
        var y = 43;
        Main.x = 5;
        proc foo() {}
    }

    module Zzz {
        use Zun;
        var y = 43;
        Main.x = 5;
        proc foo() {}
    }

    proc foo() {}
    proc fuck() {}
    proc va() {}

    var x = 23;
    x += 34;

    var i;

    {
        var y = 34;
        y = 43;
    }

    i = 6;
    y = 54;

    var y = 5;
    y = 342;
    i += 9;
    y = 54;

    proc foo() {}

    config const printLocaleName = true;
    printLocaleName = false;
    foo();
    Zun.foo();
}