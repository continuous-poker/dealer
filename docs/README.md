# Continuous Poker

This is a workshop idea to showcase the benefits of continuous integration and deployment in software development.

Up to 10 teams compete in a game of poker, by developing a poker player software that adapts to the game situation. Not everyone in a team needs to be a coder - it might also be helpful to have someone analyze the game strategy or manage the group skills.

## Prerequisites

* Kubernetes Cluster with Ingress support
* at least one GitHub Account for each team

## How it works

Continuous Poker utilizes GitHub Workflows and Kubernetes to automatically build and deploy the player bots.

The facilitator needs to prepare the cluster und provide each team with access to it. The dealer software needs to be deployed before the workshop. See the [Continuous Poker Dealer GitHub page](https://github.com/continuous-poker/dealer) for details.

The teams can then choose from one of the base implementations to implement their player bot:

* [Java (Quarkus)](https://github.com/continuous-poker/player-quarkus)
* [Java (Spring Boot)](https://github.com/continuous-poker/player-spring-boot)
* [Go (Gin)](https://github.com/continuous-poker/player-go-gin)
* [TypeScript](https://github.com/continuous-poker/player-typescript)
* [JavaScript](https://github.com/continuous-poker/player-javascript)
* [Python (Flask)](https://github.com/continuous-poker/player-python-flask)
* [C#/.NET](https://github.com/continuous-poker/player-dotnet)
* [C](https://github.com/continuous-poker/player-c)
* [Rust (Rocket)](https://github.com/continuous-poker/player-rust-rocket)

The base implementations already come with a GitHub workflow to deploy the bot. The players just need to set the required secrets and variables in their forked repository:

* TEAMNAME - each good team needs a name to unite under 😀
* TOKEN - the content of the kubeconfig.yaml file, so the cluster can be accessed
* NAMESPACE - the namespace to use on the cluster

As soon as all teams were able to deploy their basic bots, the facilitator can start the game and the teams can start coding. It's nice to pause the game from time to time and have the teams talk about their approach on the game.

Maybe there is even a prize to win for the best team?

## Goals of the workshop

* Improving teamwork
* Learning from each other
* Acknowledging the benefits of quick deployments
* Having fun! ❤
