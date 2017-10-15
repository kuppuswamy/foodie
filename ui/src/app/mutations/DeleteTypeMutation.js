import {
  commitMutation,
  graphql,
} from 'react-relay';
import {ConnectionHandler} from 'relay-runtime';

const mutation = graphql`
  mutation DeleteTypeMutation($input: TypeDeleteInput!) {
    deleteType(input: $input) {
      deletedTypeId
    }
  }
`;

let sharedUpdater = (store, deletedID) => {
  const conn = ConnectionHandler.getConnection(
    store.getRoot(),
    'TypesList_types',
  );
  ConnectionHandler.deleteNode(conn, deletedID);
};

let commit = (environment, type) => {
  return commitMutation(
    environment,
    {
      mutation,
      variables: {
        input: {id: type.id},
      },
      updater: (store) => {
        const payload = store.getRootField('deleteType');
        sharedUpdater(store, payload.getValue('deletedTypeId'));
      },
      optimisticUpdater: (store) => {
        sharedUpdater(store, type.id);
      },
    }
  );
};

export default {commit};
