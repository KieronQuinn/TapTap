/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kieronquinn.app.taptap.utils.logging

/**
 * Logging interface for UI events. Normal implementation is UiEventLoggerImpl.
 * For testing, use fake implementation UiEventLoggerFake.
 *
 * See go/sysui-event-logs and UiEventReported atom in atoms.proto.
 */
interface UiEventLogger {
    /** Put your Event IDs in enums that implement this interface, and document them using the
     * UiEventEnum annotation.
     * Event IDs must be globally unique. This will be enforced by tooling (forthcoming).
     * OEMs should use event IDs above 100000 and below 1000000 (1 million).
     */
    interface UiEventEnum {
        val id: Int
    }

    /**
     * Log a simple event, with no package information. Does nothing if event.getId() <= 0.
     * @param event an enum implementing UiEventEnum interface.
     */
    fun log(event: UiEventEnum)

    /**
     * Log an event with package information. Does nothing if event.getId() <= 0.
     * Give both uid and packageName if both are known, but one may be omitted if unknown.
     * @param event an enum implementing UiEventEnum interface.
     * @param uid the uid of the relevant app, if known (0 otherwise).
     * @param packageName the package name of the relevant app, if known (null otherwise).
     */
    fun log(event: UiEventEnum, uid: Int, packageName: String?)

    /**
     * Log an event with package information and an instance ID.
     * Does nothing if event.getId() <= 0.
     * @param event an enum implementing UiEventEnum interface.
     * @param uid the uid of the relevant app, if known (0 otherwise).
     * @param packageName the package name of the relevant app, if known (null otherwise).
     * @param instance An identifier obtained from an InstanceIdSequence. If null, reduces to log().
     */
    fun logWithInstanceId(
        event: UiEventEnum, uid: Int, packageName: String?,
        instance: InstanceId?
    )

    /**
     * Log an event with ranked-choice information along with package.
     * Does nothing if event.getId() <= 0.
     * @param event an enum implementing UiEventEnum interface.
     * @param uid the uid of the relevant app, if known (0 otherwise).
     * @param packageName the package name of the relevant app, if known (null otherwise).
     * @param position the position picked.
     */
    fun logWithPosition(
        event: UiEventEnum, uid: Int, packageName: String?,
        position: Int
    )

    /**
     * Log an event with ranked-choice information along with package and instance ID.
     * Does nothing if event.getId() <= 0.
     * @param event an enum implementing UiEventEnum interface.
     * @param uid the uid of the relevant app, if known (0 otherwise).
     * @param packageName the package name of the relevant app, if known (null otherwise).
     * @param instance An identifier obtained from an InstanceIdSequence. If null, reduces to
     * logWithPosition().
     * @param position the position picked.
     */
    fun logWithInstanceIdAndPosition(
        event: UiEventEnum, uid: Int,
        packageName: String?, instance: InstanceId?, position: Int
    )
}