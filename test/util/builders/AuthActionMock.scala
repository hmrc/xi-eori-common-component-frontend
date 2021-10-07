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

package util.builders

import org.scalatest.WordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{AnyContentAsEmpty, DefaultActionBuilder}
import play.api.test.Helpers.stubBodyParser
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.{AuthAction, GroupEnrolmentExtractor}
import util.Injector

import scala.concurrent.ExecutionContext.global

trait AuthActionMock extends WordSpec with MockitoSugar with Injector {

  val configuration               = instanceOf[Configuration]
  val environment                 = Environment.simple()
  val mockGroupEnrolmentExtractor = mock[GroupEnrolmentExtractor]
  val actionBuilder               = DefaultActionBuilder(stubBodyParser(AnyContentAsEmpty))(global)

  def authAction(authConnector: AuthConnector) =
    new AuthAction(configuration, environment, authConnector, actionBuilder, mockGroupEnrolmentExtractor)(global)

}
