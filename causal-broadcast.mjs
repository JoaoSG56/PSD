#!/usr/bin/env node

import { log as out, error as log } from 'console';
import { createInterface } from 'readline';
import { strict as assert } from 'assert';
import { Worker } from 'cluster';

let json = JSON.stringify;
createInterface(process.stdin).on('line', line => {
  log("received " + line);
  handle(JSON.parse(line));
});

var node_id = "";
var node_ids = [];
var msg_id = 0;
var pending = [];

function send(node, body) {
  msg_id += 1;
  let msg = json({src: node_id, dest: node, body: {...body, msg_id: msg_id}});
  out(msg);
  log("sent " + msg);
}

function broadcast(body) {
  for (const node of node_ids)
    if (node != node_id)
      send(node, body);
}

function reply(request, body) {
  send(request.src, {...body, in_reply_to: request.body.msg_id});
}

function handle(msg) {
  // Dicionário de funções, quase como um switch case
  let handlers = {
    init: handle_init,
    cbcast: handle_cbcast,
    fwd_msg: handle_fwd_msg,
  };
  handlers[msg.body.type](msg);
}

var vv = {};
var msgs = [];

function handle_init(msg) {
  node_id = msg.body.node_id;
  node_ids = msg.body.node_ids;
  for (const i of node_ids)
    vv[i] = 0;
  reply(msg, {type: 'init_ok'});
  log("init sender: " + msg.src);
}

function handle_cbcast(msg) {
  vv[node_id] += 1;
  reply(msg, {type: 'cbcast_ok', messages: msgs});
  msgs = [];
  broadcast({type: 'fwd_msg', vv: vv, message: msg.body.message});
}

function check(msg) {

  let condition = vv[msg.src] + 1 == msg.body.vv[msg.src];
  let condition2 = false;

  if (msg.src != node_id){
    if(msg.body.vv[msg.src] <= vv[msg.src])
      condition2 = true;
  }
  return condition && condition2;
}

function handle_fwd_msg(msg){
  
  if(check(msg)){
    msgs.push(msg.body.message)
    vv[node_id] += 1
    let count = 0
    for(var m of pending){
      if(check(m)){
        msgs.push(m.body.message)
        vv[m.src] += 1
        count += 1;
      }
      else
      {
        break;
      }
    }
    if (count > 0) pending = pending.slice(0,count)
  }

  else 
  {
    pending.push(msg)
  }
}

