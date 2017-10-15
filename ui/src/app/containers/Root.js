import React from 'react';
import {QueryRenderer, graphql} from 'react-relay/index';
import environment from '../environment'
import Lists from './Lists';
import Header from '../components/Header';
import Footer from '../components/Footer';

class Root extends React.Component {
  render = () => {
    return (
      <QueryRenderer
        environment={environment}
        query={graphql`
            query RootQuery {
              ...Lists_store
            }
          `
        }
        render={
          ({error, props}) => {
            let elems = [<Header/>];
            if (props)
              elems.push(<Lists store={props}/>);
            else
              elems.push(<section className="hero is-medium">
                <div className="hero-body">
                  <div className="container has-text-centered">
                    <p className="title">
                      Loading...
                    </p>
                  </div>
                </div>
              </section>);
            elems.push(<Footer/>);
            return elems;
          }
        }
      />
    );
  }
}

export default Root;