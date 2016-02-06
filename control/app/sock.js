import SockJS from '../node_modules/sockjs-client'


export default class Sock {
    constructor(){
        var sock = new SockJS('http://192.168.1.5:9999/echo');

        sock.onopen = function() {
            console.log('open');
        };

        sock.onmessage = function(e) {
            console.log('message', e.data);
        };

        sock.onclose = function() {
            console.log('close');
        };

        this.sock = sock;
    }

    send(cmd){
        this.sock.send(cmd);
    }
}

