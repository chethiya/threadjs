ForkThread = (require './../../fork_thread').ForkThread
util = (require './../../util')
util.setId '[Fork Child]'
logUser = util.logUser

child = new ForkThread parent: false, program: null, params: []

child.onRecord = (data, callback) ->
 console.log data
 callback()

child.onStarted (err, data) ->
 console.log "child started : ", err

 child.send 'getRecord', 'test', (err, data) ->
  console.log "Data recieved from parent process", err, data

test = ->
 console.log 'check'
setInterval test, 10000
