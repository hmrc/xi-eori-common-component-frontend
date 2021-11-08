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

package config

import common.pages.RegistrationPage
import org.scalatest.concurrent.ScalaFutures
import play.api.Configuration
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, _}
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionTimeOutException
import uk.gov.hmrc.xieoricommoncomponentfrontend.config.ErrorHandler
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{client_error_template, error_template, notFound}
import util.BaseSpec

class ErrorHandlerSpec extends BaseSpec with ScalaFutures {
  val configuration = mock[Configuration]

  private val errorTemplateView       = instanceOf[error_template]
  private val clientErrorTemplateView = instanceOf[client_error_template]
  private val notFoundView            = instanceOf[notFound]

  val errorHandler =
    new ErrorHandler(errorTemplateView, notFoundView, clientErrorTemplateView, messagesApi)

  private val mockRequest = FakeRequest()

  "Cds error handler" should {
    "redirect to correct page after receive 500 error" in {

      val result = errorHandler.onServerError(mockRequest, new Exception())
      val page   = RegistrationPage(contentAsString(result))
      status(result) shouldBe INTERNAL_SERVER_ERROR
      page.title should startWith("Sorry, there is a problem with the service")

    }

    "redirect to registration security sign out" in {
      val result = errorHandler.onServerError(mockRequest, SessionTimeOutException("xyz"))
      status(result) shouldBe SEE_OTHER
      val response = await(result)
      response.header.headers(LOCATION) endsWith "/sign-out"
    }

    "Redirect to the notfound page on 404 error" in {
      val result = errorHandler.onClientError(mockRequest, statusCode = NOT_FOUND)
      val page   = RegistrationPage(contentAsString(result))
      status(result) shouldBe NOT_FOUND
      page.title should startWith("Page not found")

    }

    "Redirect to the InternalErrorPage page on 500 error" in {
      val result = errorHandler.onClientError(mockRequest, statusCode = INTERNAL_SERVER_ERROR)
      val page   = RegistrationPage(contentAsString(result))
      status(result) shouldBe INTERNAL_SERVER_ERROR
      page.title should startWith("Something went wrong. Please try again later.")
    }

    /*


        "Redirect to the notfound page on 404 error with InvalidPathParameter" in {
          whenReady(
            errorHandler.onClientError(mockRequest, statusCode = BAD_REQUEST, message = Constants.INVALID_PATH_PARAM)
          ) { result =>
            val page = RegistrationPage(contentAsString(result))

            result.header.status shouldBe NOT_FOUND
            page.title should startWith("Page not found")
          }
        }

        "Redirect to the InternalErrorPage page on 500 error" in {
          whenReady(errorHandler.onClientError(mockRequest, statusCode = INTERNAL_SERVER_ERROR)) { result =>
            val page = RegistrationPage(contentAsString(result))

            result.header.status shouldBe INTERNAL_SERVER_ERROR
            page.title should startWith("Something went wrong. Please try again later.")
          }
        }

        "throw exception for unused method" in {

          intercept[IllegalStateException] {
            errorHandler.standardErrorTemplate("", "", "")(FakeRequest())
          }
        }*/
  }
}
