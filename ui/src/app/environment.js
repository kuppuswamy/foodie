import {Environment, Network, RecordSource, Store} from 'relay-runtime'
import fetch from 'isomorphic-fetch';

let fetchQuery = (operation, variables, cacheConfig, uploadables) => {
  return fetch('/graphql', {
    method: 'POST',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      query: operation.text,
      variables,
    })
  }).then(response => {
    return response.json();
  });
};

const env = new Environment({
  network: Network.create(fetchQuery),
  store: new Store(new RecordSource()),
});

export default env;