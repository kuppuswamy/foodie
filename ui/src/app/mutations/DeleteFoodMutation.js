import {
  commitMutation,
  graphql,
} from 'react-relay';
import {ConnectionHandler} from 'relay-runtime';

const mutation = graphql`
  mutation DeleteFoodMutation($input: FoodDeleteInput!) {
    deleteFood(input: $input) {
      deletedFoodId
    }
  }
`;

let sharedUpdater = (store, deletedID) => {
  const conn = ConnectionHandler.getConnection(
    store.getRoot(),
    'FoodsList_foods',
  );
  ConnectionHandler.deleteNode(conn, deletedID);
};

let commit = (environment, food) => {
  return commitMutation(
    environment,
    {
      mutation,
      variables: {
        input: {id: food.id},
      },
      updater: (store) => {
        const payload = store.getRootField('deleteFood');
        sharedUpdater(store, payload.getValue('deletedFoodId'));
      },
      optimisticUpdater: (store) => {
        sharedUpdater(store, food.id);
      },
    }
  );
};

export default {commit};
