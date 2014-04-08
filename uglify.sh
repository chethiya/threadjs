echo $1
uglifyjs $1 -m -o temp.js
mv temp.js $1
