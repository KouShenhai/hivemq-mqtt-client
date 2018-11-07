/*
 * Copyright 2018 The MQTT Bee project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.mqttbee.mqtt.message.disconnect;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mqttbee.api.mqtt.datatypes.MqttUTF8String;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import org.mqttbee.api.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder;
import org.mqttbee.api.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode;
import org.mqttbee.mqtt.datatypes.MqttUTF8StringImpl;
import org.mqttbee.mqtt.datatypes.MqttUserPropertiesImpl;
import org.mqttbee.mqtt.util.MqttBuilderUtil;
import org.mqttbee.util.Checks;
import org.mqttbee.util.UnsignedDataTypes;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Silvio Giebl
 */
public abstract class MqttDisconnectBuilder<B extends MqttDisconnectBuilder<B>> {

    private @NotNull Mqtt5DisconnectReasonCode reasonCode = MqttDisconnect.DEFAULT_REASON_CODE;
    private long sessionExpiryIntervalSeconds = MqttDisconnect.SESSION_EXPIRY_INTERVAL_FROM_CONNECT;
    private @Nullable MqttUTF8StringImpl serverReference;
    private @Nullable MqttUTF8StringImpl reasonString;
    private @NotNull MqttUserPropertiesImpl userProperties = MqttUserPropertiesImpl.NO_USER_PROPERTIES;

    abstract @NotNull B self();

    public @NotNull B reasonCode(final @NotNull Mqtt5DisconnectReasonCode reasonCode) {
        this.reasonCode = Checks.notNull(reasonCode, "Reason Code");
        return self();
    }

    public @NotNull B sessionExpiryInterval(final long sessionExpiryInterval, final @NotNull TimeUnit timeUnit) {
        final long sessionExpiryIntervalSeconds = timeUnit.toSeconds(sessionExpiryInterval);
        Preconditions.checkArgument(UnsignedDataTypes.isUnsignedInt(sessionExpiryIntervalSeconds),
                "The value of session expiry interval converted in seconds must not exceed the value range of unsigned int. Found: %s which is bigger than %s (max unsigned int).",
                sessionExpiryInterval, UnsignedDataTypes.UNSIGNED_INT_MAX_VALUE);

        this.sessionExpiryIntervalSeconds = sessionExpiryIntervalSeconds;
        return self();
    }

    public @NotNull B serverReference(final @Nullable String serverReference) {
        this.serverReference = MqttBuilderUtil.stringOrNull(serverReference);
        return self();
    }

    public @NotNull B serverReference(final @Nullable MqttUTF8String serverReference) {
        this.serverReference = MqttBuilderUtil.stringOrNull(serverReference);
        return self();
    }

    public @NotNull B reasonString(final @Nullable String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return self();
    }

    public @NotNull B reasonString(final @Nullable MqttUTF8String reasonString) {
        this.reasonString = MqttBuilderUtil.stringOrNull(reasonString);
        return self();
    }

    public @NotNull B userProperties(final @NotNull Mqtt5UserProperties userProperties) {
        this.userProperties = MqttBuilderUtil.userProperties(userProperties);
        return self();
    }

    public @NotNull Mqtt5UserPropertiesBuilder<B> userProperties() {
        return new Mqtt5UserPropertiesBuilder<>(this::userProperties);
    }

    public @NotNull MqttDisconnect build() {
        return new MqttDisconnect(
                reasonCode, sessionExpiryIntervalSeconds, serverReference, reasonString, userProperties);
    }

    public static class Default extends MqttDisconnectBuilder<Default> implements Mqtt5DisconnectBuilder {

        @Override
        @NotNull Default self() {
            return this;
        }
    }

    public static class Nested<P> extends MqttDisconnectBuilder<Nested<P>> implements Mqtt5DisconnectBuilder.Nested<P> {

        private final @NotNull Function<? super MqttDisconnect, P> parentConsumer;

        public Nested(final @NotNull Function<? super MqttDisconnect, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Nested<P> self() {
            return this;
        }

        @Override
        public @NotNull P applyDisconnect() {
            return parentConsumer.apply(build());
        }
    }

    public static class Send<P> extends MqttDisconnectBuilder<Send<P>> implements Mqtt5DisconnectBuilder.Send<P> {

        private final @NotNull Function<? super MqttDisconnect, P> parentConsumer;

        public Send(final @NotNull Function<? super MqttDisconnect, P> parentConsumer) {
            this.parentConsumer = parentConsumer;
        }

        @Override
        @NotNull Send<P> self() {
            return this;
        }

        @Override
        public @NotNull P send() {
            return parentConsumer.apply(build());
        }
    }
}
