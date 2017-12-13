import Foods from '../components/Foods';
import {createRefetchContainer, graphql} from 'react-relay';

export const COUNT = 2;

export default createRefetchContainer(Foods,
  {
    foodStore: graphql`
      fragment Foods_foodStore on Query
      @argumentDefinitions(
        first: {type: "Int", defaultValue: 2},
        after: {type: "String"},
        last: {type: "Int"}
        before: {type: "String"},
        sort: {type: "String", defaultValue: "desc"}
      ) {
        foods (first: $first, after: $after, last: $last, before: $before, sort: $sort) {
          pageInfo {
            hasNextPage
            hasPreviousPage
            startCursor
            endCursor
          }
          count
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
  graphql`
    query FoodsPaginationQuery($first: Int, $after: String, $last: Int, $before: String, $sort: String) {
      ...Foods_foodStore @arguments(first: $first, after: $after, last: $last, before: $before, sort: $sort)
    }
  `
);