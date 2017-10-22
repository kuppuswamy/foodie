import {
  commitMutation,
  graphql,
} from 'react-relay';

const mutation = graphql`
  mutation EditTypeMutation($input: TypeEditInput!) {
    editType(input:$input) {
      type {
        id
        name
      }
    }
  }
`;

function getOptimisticResponse(type) {
  return {
    editType: {
      type: {
        id: type.id,
        name: type.name,
      },
    },
  };
}

function commit(
  environment,
  type
) {
  return commitMutation(
    environment,
    {
      mutation,
      variables: {
        input: {name: type.name, id: type.id},
      },
      optimisticResponse: getOptimisticResponse(type),
    }
  );
}

export default {commit};
