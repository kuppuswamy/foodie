import {
  commitMutation,
  graphql,
} from 'react-relay';
import {ConnectionHandler} from 'relay-runtime';

const mutation = graphql`
  mutation AddFoodMutation($input: FoodAddInput!) {
    addFood(input: $input) {
      __typename
      cursor
      node {
        id
        name
        type {
          id
          name
        }
      }
    }
  }
`;

let sharedUpdater = (store, newEdge) => {
  const conn = ConnectionHandler.getConnection(
    store.getRoot(),
    'FoodsList_foods'
  );
  ConnectionHandler.insertEdgeBefore(conn, newEdge);
};

let tempID = 0;

let commit = (environment, name, type_id) => {
  return commitMutation(
    environment,
    {
      mutation,
      variables: {
        input: {
          name,
          type_id
        },
      },
      updater: (store) => {
        const newEdge = store.getRootField('addFood');
        sharedUpdater(store, newEdge);
      },
      optimisticUpdater: (store) => {
        const id = 'client:newFood:' + tempID++;
        const node = store.create(id, 'Food');
        node.setValue(name, 'name');
        node.setValue(id, 'id');
        node.setLinkedRecord(store.get(type_id), 'type');
        const newEdge = store.create(
          'client:newEdge:' + tempID++,
          'FoodEdge',
        );
        newEdge.setLinkedRecord(node, 'node');
        sharedUpdater(store, newEdge);
      },
    }
  );
};

export default {commit};
