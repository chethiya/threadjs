threadjs
========

Extendable set of classes written in coffeescript and Java (few more languages to be added) so that coffeescript can call (and spawn) other processes. And also can call other process in similar to calling async methods in an object.

E.g.

In NodeJS program
-----------------

JavaThread = (require './java_thread').JavaThread

javaProgram = new JavaThread
 server: true # Whether node acts as server or client. At the moment java does't support being a serverJavaThread is based on sockets. 
 spawn: true # create the java process
 cwd: "./java/threadjs"
 program: "java"
 params: [
  "-cp"
  "target/uber-threadjs-1.0-SNAPSHOT.jar"
  "org.forestpin.threadjs.App"
 ]

javaProgram.onRecord = (data, callback) ->
 console.log data
 callback()

javaProgram.onStarted (err, data) ->
 console.log "Java program is started and reday to receive message: "

 javaProgram.send 'getRecord', {}, (err, data) ->
  console.log "Java program sent a callback. Java 'getRecord' method is supposed to call the onRecord method here"


In Java program
---------------

package org.forestpin.threadjs;

import net.sf.json.JSONObject;

public class Messager extends ThreadJS {

    public void getRecord(JSONObject data, Callback cb) {
        System.out.println("[java] getRecord is called");
        cb.callback(null, data);

        JSONObject obj = JSONObject.fromObject("{vendor: \"pamona\", amount: 12313}");
        for (int i=0; i<10000; ++i) {
            send("onRecord", obj, null);
        }
    }
}



