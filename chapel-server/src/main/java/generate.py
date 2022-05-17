import subprocess
subprocess.run(["jjtree", "parser.jjt"])
subprocess.run(["javacc", "-OUTPUT_DIRECTORY:parser", "parser/parser.jj"])
# subprocess.run(["javac", "Parser.java"])
