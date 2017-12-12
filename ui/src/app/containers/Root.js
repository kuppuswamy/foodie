import React from 'react';
import {QueryRenderer, graphql} from 'react-relay/index';
import environment from '../environment'
import Foods from './Foods';
import Types from './Types';
import Header from '../components/Header';
import Footer from '../components/Footer';

class Root extends React.Component {
  render = () => {
    const Aux = props => props.children;
    return (
      <QueryRenderer
        environment={environment}
        query={
          graphql`
            query RootQuery($count: Int!, $cursor: String, $sort: String) {
              ...Foods_foodStore
              ...Foods_typeStore
              ...Types_typeStore
            }
          `
        }
        variables={{count: 2, cursor: null, sort: 'desc'}}
        render={
          ({error, props}) => {
            let elems = [<Header key={1}/>];
            if (props)
              elems.push(
                <Aux key={2}>
                  <section className="section">
                    <div className="container">
                      <div className="columns">
                        <div className="column">
                          <Types typeStore={props}/>
                        </div>
                        <div className="column">
                          <Foods foodStore={props} typeStore={props}/>
                        </div>
                      </div>
                    </div>
                  </section>
                </Aux>
              );
            else
              elems.push(
                <section key={2} className="hero is-medium">
                  <div className="hero-body">
                    <div className="container has-text-centered">
                      <p className="title">
                        Loading...
                      </p>
                    </div>
                  </div>
                </section>
              );
            elems.push(<Footer key={4}/>);
            return elems;
          }
        }
      />
    );
  }
}

export default Root;