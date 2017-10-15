import React from 'react';
import Foods from './Foods';
import Types from './Types';

export default class Lists extends React.Component {
  render = () => {
    let {store, relay} = this.props;
    const Aux = props => props.children;
    return (
      <Aux>
        <section className="section">
          <div className="container">
            <div className="columns">
              <div className="column">
                <Types types={store.types} relay={relay}/>
              </div>
              <div className="column">
                <Foods foods={store.foods} types={store.types} relay={relay}/>
              </div>
            </div>
          </div>
        </section>
      </Aux>
    );
  }
}