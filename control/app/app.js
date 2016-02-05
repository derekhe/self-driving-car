import React from 'react';
import styles from './app.css';

export default class App extends React.Component {
  constructor(props) {
    super(props);
  }
  render() {
    return (
      <div className={styles.app}>
        Car
      </div>
    );
  }
}
