FROM mcr.microsoft.com/devcontainers/java:17

USER root
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends erlang-base rebar3 \
    && rm -rf /var/lib/apt/lists/*
USER vscode