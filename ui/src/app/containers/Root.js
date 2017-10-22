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
            let elems = [<Header key={1}/>];
            if (props)
              elems.push(<Lists key={2} store={props}/>);
            else
              elems.push(<section key={2} className="hero is-medium">
                <div className="hero-body">
                  <div className="container has-text-centered">
                    <p className="title">
                      Loading...
                    </p>
                  </div>
                </div>
              </section>);
            elems.push(<Footer key={3}/>);
            return elems;
          }
        }
      />
    );
  }
}

export default Root;