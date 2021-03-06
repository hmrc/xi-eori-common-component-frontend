/*
 * Copyright 2021 HM Revenue & Customs
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
 */

package uk.gov.hmrc.xieoricommoncomponentfrontend.domain

import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}

sealed trait LoggedInUser {
  def affinityGroup: Option[AffinityGroup]
  def internalId: Option[String]

  lazy val isAgent: Boolean = affinityGroup.contains(AffinityGroup.Agent)

  def userAffinity(): AffinityGroup = affinityGroup match {
    case Some(id) => id
    case _        => throw new IllegalStateException("No affinity group returned by Government Gateway.")
  }

}

case class LoggedInUserWithEnrolments(
  affinityGroup: Option[AffinityGroup],
  internalId: Option[String],
  enrolments: Enrolments,
  email: Option[String],
  groupId: Option[String]
) extends LoggedInUser
