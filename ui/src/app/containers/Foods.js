import Foods from '../components/Foods';
import {createRefetchContainer, graphql} from 'react-relay';

export default createRefetchContainer(Foods,
  {
    foodStore: graphql `
      fragment Foods_foodStore on Query
      @argumentDefinitions(
        sort: {type: "String", defaultValue: "desc"}
      ) {
        foods (first: 2147483647, sort: $sort) @connection(key: "FoodsList_foods", filters: []) {
          edges {
            node {
              name
              id
              type {
                name
                id
              }
            }
            cursor
          }
        }
      }
    `,
    typeStore: graphql`
      fragment Foods_typeStore on Query {
        typesForAddFood: types (first: 2147483647, sort: "desc") @connection(key: "TypesList_typesForAddFood", filters: []) {
          edges {
            node {
              name
              id
            }
            cursor
          }
        }
      }
    `
  },
  graphql`
    query FoodsRefetchQuery($sort: String) {
      ...Foods_foodStore @arguments(sort: $sort)
    }
  `,
);