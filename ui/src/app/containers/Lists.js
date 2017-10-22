import Lists from '../components/Lists';
import {createFragmentContainer, graphql} from 'react-relay/index';

export default createFragmentContainer(Lists,
  graphql`
    fragment Lists_store on Query {
      foods (first: 2147483647, sort: "desc") @connection(key: "FoodsList_foods", filters: []) {
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
      types (first: 2147483647, sort: "desc") @connection(key: "TypesList_types", filters: []) {
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
);