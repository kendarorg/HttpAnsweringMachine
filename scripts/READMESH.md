
* releasebuild.sh : Builds a tar.gz with a localhost Ham version (ready to be uploaded on the author github)
* deploy.sh : Builds and deploy on author maven repository


# File system

Script directory
<pre>
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
</pre>

Current directory (execution)
<pre>
START_LOCATION=$(pwd)
</pre>

# Redirects

Redirect -everything- to file
<pre>
mvn clean install > "$ROOT_DIR"/release/ham-"$HAM_VERSION".log 2>1
</pre>

# Functions

Return values

<pre>
my_function () {
  local func_result="some result"
  echo "$func_result"
}

func_result="$(my_function)"
echo $func_result
</pre>

Passing values

<pre>
greeting () {
  echo "Hello $1"
}

greeting "Joe"
</pre>