package com.jraska.rx.codelab;

import com.jraska.rx.codelab.http.*;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.jraska.rx.codelab.Utils.sleep;

public class Task5_IntoPractice_GitHub {
  private static final String LOGIN = "defunkt"; // One of GitHub founders. <3 GitHub <3

  GitHubApi gitHubApi;

  @Before
  public void before() {
    gitHubApi = HttpModule.mockedGitHubApi();
  }

  @Test
  public void map_printUser() {
    Observable<GitHubUser> userObservable = gitHubApi.getUser(LOGIN);

    // TODO Map GitHubUser object into User and print it out. User has toString implemented.
    // NOTE: You can find GitHubConverter useful for converting between different object types.

    userObservable
      .map(GitHubConverter::convert)
      .subscribe(System.out::println);
  }

  @Test
  public void flatMap_getFirstUserAndPrintHim() {
    Observable<List<GitHubUser>> firstUsers = gitHubApi.getFirstUsers();

//    Observable<GitHubUser> userObservable = gitHubApi.getUser(loginVariable);

    // TODO Pick first user 'login' from the list. Perform another request and print the user.

    Observable<GitHubUser> gitHubUserObservable = firstUsers.flatMap(new Function<List<GitHubUser>, ObservableSource<GitHubUser>>() {
      @Override
      public ObservableSource<GitHubUser> apply(List<GitHubUser> gitHubUsers) throws Exception {
        return gitHubApi.getUser(gitHubUsers.get(0).login);
      }
    });

    Observable<User> userObservable = gitHubUserObservable.map(GitHubConverter::convert);

    userObservable.subscribe(System.out::println);
  }

  @Test
  public void zip_getUserAndHisRepos() {
    Observable<GitHubUser> userObservable = gitHubApi.getUser(LOGIN)
      .subscribeOn(Schedulers.io());
    Observable<List<GitHubRepo>> reposObservable = gitHubApi.getRepos(LOGIN);

    // TODO Get User with his Repos to create Observable<UserWithRepos> and print the user with repos.
    // NOTE: You can find GitHubConverter useful for converting between different object types.

    Observable<UserWithRepos> userWithReposObservable = userObservable.zipWith(reposObservable, new BiFunction<GitHubUser, List<GitHubRepo>, UserWithRepos>() {
      @Override
      public UserWithRepos apply(GitHubUser gitHubUser, List<GitHubRepo> gitHubRepos) throws Exception {
        return new UserWithRepos(GitHubConverter.convert(gitHubUser), GitHubConverter.convert(gitHubRepos));
      }
    });

    Observable<UserWithRepos> userWithReposLambda = userObservable.zipWith(reposObservable,
      (gitHubUser, gitHubRepos) -> new UserWithRepos(GitHubConverter.convert(gitHubUser), GitHubConverter.convert(gitHubRepos)));

    userWithReposObservable.blockingSubscribe(System.out::println);
  }

  @Test
  public void zip_subscribeOn_twoUserAndReposInParallel() {
    Observable<GitHubUser> userObservable = gitHubApi.getUser(LOGIN);
    Observable<List<GitHubRepo>> reposObservable = gitHubApi.getRepos(LOGIN);

    // TODO: Get User with his Repos to in parallel to create Observable<UserWithRepos> and print the user with repos.
    // NOTE: Use Thread.sleep to keep the unit test running, or you can use blockingSubscribe from RxJava
  }

  @Test
  public void zip_subscribeOn_twoSerialRequestsWithScheduler() {
    Observable<GitHubUser> userObservable = gitHubApi.getUser(LOGIN);
    Observable<List<GitHubRepo>> reposObservable = gitHubApi.getRepos(LOGIN);

    // TODO: Use Schedulers.single() to run requests from previous test in serial order, but be scheduled out of current thread
    // NOTE: Use Thread.sleep to keep the unit test running, or you can use blockingSubscribe from RxJava

    sleep(2000); // In real code the application just continues, but this is unit test
  }

  @Test
  public void subscribeOn_observeOn_printRequestsFromDifferentThread() {
    Observable<GitHubUser> userObservable = gitHubApi.getUser(LOGIN);
    Observable<List<GitHubRepo>> reposObservable = gitHubApi.getRepos(LOGIN);

    // TODO: Lets play around and make the requests run in parallel, but log the emits from the same thread. You can use printWithThreadId to check that

    printWithThreadId("Current thread");

    sleep(2000); // In real code the application just continues, but this is unit test
  }

  void printWithThreadId(Object object) {
    System.out.println("Thread id: " + Thread.currentThread().getId() + ", " + object);
  }

}
