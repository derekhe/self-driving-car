import React from 'react';
import ReactDOM from 'react-dom';
import App from './app.js';
import SockJS from '../node_modules/sockjs-client'

ReactDOM.render(<App />, document.getElementById('root'));

var sock = new SockJS('http://localhost:9999/echo');
sock.onopen = function() {
    console.log('open');
};
sock.onmessage = function(e) {
    console.log('message', e.data);
};
sock.onclose = function() {
    console.log('close');
};