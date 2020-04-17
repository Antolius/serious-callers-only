package hr.from.josipantolis.seriouscallersonly.api

import java.time.Instant

sealed class Event(
    open val channel: Channel,
    open val happenedAt: Instant
) {

    data class Timer(
        override val channel: Channel,
        override val happenedAt: Instant
    ) : Event(channel, happenedAt)

    data class UserJoinedChannel(
        override val channel: Channel,
        override val happenedAt: Instant,
        val user: User
    ) : Event(channel, happenedAt)

    data class PublicMessagePosted(
        override val channel: Channel,
        override val happenedAt: Instant,
        val author: User,
        val content: MessageText
    ) : Event(channel, happenedAt)

    data class CommandInvoked(
        override val channel: Channel,
        override val happenedAt: Instant,
        val command: Command,
        val invoker: User,
        val invocationText: String
    ) : Event(channel, happenedAt)

    data class BotJoinedChannel(
        override val channel: Channel,
        override val happenedAt: Instant
    ) : Event(channel, happenedAt)

    data class PrivateMessageReceived(
        override val channel: Channel,
        override val happenedAt: Instant,
        val author: User,
        val content: MessageText
    ) : Event(channel, happenedAt)

    data class HomeTabVisited(
        override val channel: Channel,
        override val happenedAt: Instant,
        val visitor: User
    ) : Event(channel, happenedAt)


    sealed class Interaction(channel: Channel, happenedAt: Instant) : Event(channel, happenedAt) {

        data class ButtonClicked(
            override val channel: Channel,
            override val happenedAt: Instant,
            val clickedBy: User
        ) : Interaction(channel, happenedAt)

        data class OptionPicked(
            override val channel: Channel,
            override val happenedAt: Instant,
            val pickedBy: User
        ) : Interaction(channel, happenedAt)

        data class TextInput(
            override val channel: Channel,
            override val happenedAt: Instant,
            val author: User,
            val text: InputText
        ) : Interaction(channel, happenedAt)

        data class UserResponded(
            override val channel: Channel,
            override val happenedAt: Instant,
            val author: User,
            val content: MessageText
        ) : Interaction(channel, happenedAt)
    }

}
