import { act, render } from '@testing-library/react';
import { StoreProvider } from 'easy-peasy';
import { MockedProvider } from "@apollo/react-testing";
import { store } from '../../services/store.service';
import { MemoryRouterProvider } from 'next-router-mock/dist/MemoryRouterProvider';
import { enableFetchMocks } from 'jest-fetch-mock'
enableFetchMocks()
jest.mock('next/router', () => require('next-router-mock'));
import fetch from 'jest-fetch-mock'
import ScenarioList from '../../pages/dco/scenario/scenarioList';
import { GET_SCENARIO } from '../../services/queries';
import { gql } from '@apollo/client';
describe('Table render in scenario', () => {
  const useRouter = jest.spyOn(require('next/router'), 'useRouter');
  useRouter.mockImplementation(() => ({
    pathname: '/dco/scenario',
  }));
  const mockList = [{
    request: {
      query: gql(GET_SCENARIO),
      variables: { scenarioPattern: 'sce', page: 0, size: 10 }
    },
    result: { data: { searchScenarioByPattern: '' } },
  }]
  test('table with props', async () => {
    store.getActions().setCount(0);
    useRouter
    render(
      //  @ts-ignore 
      <StoreProvider store={store}>
        <MemoryRouterProvider url="/dco/scenario">
          <MockedProvider mocks={mockList}>
            <ScenarioList />
          </MockedProvider>
        </MemoryRouterProvider>
      </StoreProvider>
    )
    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 0));
    });
    store.getActions().setInvert(true);
  });
  beforeEach(() => {
    fetch.resetMocks();
  });

})
