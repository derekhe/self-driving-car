import React from 'react';
import Sock from './sock.js'

export default class Pad extends React.Component {
    constructor(props) {
        super(props);
        this.sock = new Sock();
    }

    forward = () => {
        this.sock.send("forward");
    };

    backward = () => {
        this.sock.send("backward");
    };

    left = () => {
        this.sock.send("left");
    };

    right = () => {
        this.sock.send("right");
    };

    restore = () => {
        this.sock.send("pause");
    };

    render() {
        return (
            <table className="table table-bordered">
                <tbody>
                <tr className="row">
                    <td></td>
                    <td>
                        <button className="btn btn-success btn-lg btn-block" onClick={this.forward}>Forward</button>
                    </td>
                    <td></td>
                </tr>
                <tr className="row">
                    <td>
                        <button className="btn btn-success btn-lg btn-block" onClick={this.left}>Left</button>
                    </td>
                    <td>
                        <button className="btn btn-success btn-lg btn-block" onClick={this.restore}>Restore</button>
                    </td>
                    <td>
                        <button className="btn btn-success btn-lg btn-block" onClick={this.right}>Right</button>
                    </td>
                </tr>
                <tr className="row">
                    <td></td>
                    <td>
                        <button className="btn btn-success btn-lg btn-block" onClick={this.backward}>Backward</button>
                    </td>
                    <td></td>
                </tr>
                </tbody>
            </table>
        );
    }
}
