import subprocess
subprocess.run(["jjtree", "parser.jjt"])
subprocess.run(["javacc", "parser.jj"])
subprocess.run(["javac", "Parser.java"])
