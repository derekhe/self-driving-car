import React from 'react';
import Pad from './pad.js';

export default class App extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div className="container">
                <Pad/>
            </div>
        );
    }
}
