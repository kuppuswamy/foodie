import Foods from '../components/Foods';
import {createPaginationContainer, graphql} from 'react-relay';

export const COUNT = 2;

export default createPaginationContainer(Foods,
  {
    foodStore: graphql`
      fragment Foods_foodStore on Query {
        foods (first: $count, after: $cursor, sort: $sort) @connection(key: "FoodsList_foods", filters: []) {
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
        typesForAddFood: types (first: 2147483647, sort: "desc") {
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
  {
    direction: 'forward',
    getConnectionFromProps(props) {
      return props.foodStore && props.foodStore.foods;
    },
    getFragmentVariables(prevVars, totalCount) {
      return {
        ...prevVars,
        count: totalCount,
      };
    },
    getVariables(props, {count, cursor}, fragmentVariables) {
      return {
        count,
        cursor,
        sort: fragmentVariables.sort,
      };
    },
    query: graphql`
      query FoodsPaginationQuery($count: Int!, $cursor: String, $sort: String) {
        ...Foods_foodStore
      }
    `
  }
);