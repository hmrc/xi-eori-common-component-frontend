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

package uk.gov.hmrc.xieoricommoncomponentfrontend.config

import play.api.Logger
import play.api.http.Status._

import javax.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{client_error_template, error_template, notFound}

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject() (
  errorTemplate: error_template,
  notFoundView: notFound,
  clientErrorTemplateView: client_error_template,
  val messagesApi: MessagesApi
) extends FrontendErrorHandler {

  private val logger = Logger(this.getClass)

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
    request: Request[_]
  ): Html = errorTemplate()

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {

    // $COVERAGE-OFF$Loggers
    logger.error(s"Error with status code: $statusCode and message: $message")
    // $COVERAGE-ON
    implicit val req: Request[_] = Request(request, "")

    statusCode match {
      case NOT_FOUND => Future.successful(Results.NotFound(notFoundView()))
      case _         => Future.successful(Results.InternalServerError(clientErrorTemplateView(message)))
    }
  }

}
