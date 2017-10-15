import {
  commitMutation,
  graphql,
} from 'react-relay';
import {ConnectionHandler} from 'relay-runtime';

const mutation = graphql`
  mutation AddTypeMutation($input: TypeAddInput!) {
    addType(input: $input) {
      __typename
      cursor
      node {
        id
        name
      }
    }
  }
`;

let sharedUpdater = (store, newEdge) => {
  const conn = ConnectionHandler.getConnection(
    store.getRoot(),
    'TypesList_types'
  );
  ConnectionHandler.insertEdgeBefore(conn, newEdge);
};

let tempID = 0;

let commit = (environment, name) => {
  return commitMutation(
    environment,
    {
      mutation,
      variables: {
        input: {
          name
        },
      },
      updater: (store) => {
        const newEdge = store.getRootField('addType');
        sharedUpdater(store, newEdge);
      },
      optimisticUpdater: (store) => {
        const id = 'client:newType:' + tempID++;
        const node = store.create(id, 'Type');
        node.setValue(name, 'name');
        node.setValue(id, 'id');
        const newEdge = store.create(
          'client:newEdge:' + tempID++,
          'TypeEdge',
        );
        newEdge.setLinkedRecord(node, 'node');
        sharedUpdater(store, newEdge);
      },
    }
  );
};

export default {commit};