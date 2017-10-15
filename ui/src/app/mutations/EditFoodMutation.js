import {
  commitMutation,
  graphql,
} from 'react-relay';

const mutation = graphql`
  mutation EditFoodMutation($input: FoodEditInput!) {
    editFood(input:$input) {
      food {
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

function getOptimisticResponse(food, type) {
  return {
    editFood: {
      food: {
        id: food.id,
        name: food.name,
        type: {
          id: type.id,
          name: type.name
        }
      },
    },
  };
}

function commit(environment, food, type) {
  return commitMutation(
    environment,
    {
      mutation,
      variables: {
        input: {name: food.name, id: food.id, type_id: type.id},
      },
      optimisticResponse: () => getOptimisticResponse(food, type),
    }
  );
}

export default {commit};
