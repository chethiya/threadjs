spawn = (require 'child_process').spawn

PING = "_ping"
PING_INTERVAL = 1000

class Thread
 constructor: ->
  @spwan = true
  @started = false
  @working = false
  @cbs = []


  @_init()

 _init: ->
  @process = spwan @program, @params, env: @env

  start = =>
   send START, (err, data) =>
    started = true
    if err?
     console.log "Spawned program starter error: ", err
    else
     @_setMethods data.methods
     @working = true
    for cb in @cbs
     if not @working
      cb "Started with errors", P{
     else
      cb()

  cid = null
  check = ->
   send PING, (err, d) ->
    if not err?
     clearInterval cid if cid?
     start()
  check()
  cid = setInterval check, PING_INTERVAL

 _onMessage: (err, data, callback) ->
  if this[data.method]?
   this[data.method] data.data, callback
  else
   logError "Invalid method #{data.method}", data.data
   callback "Invalid method #{data.method}", {}

 _setMethods: (methods) ->
  for m in methods
   this[m] = (d, cb) =>
    send m, d, (err, data) ->
     cb err, data

 onStarted: (cb) ->
  if @started
   if not @working
    cb "Started with errors", {}
   else
    cb()
   return true
  else
   @cbs.push cb
   return false





