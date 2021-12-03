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

package uk.gov.hmrc.xieoricommoncomponentfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthAction
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ContactAddressLookupFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.contact_address_lookup

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ContactAddressLookupController @Inject() (
  authAction: AuthAction,
  formProvider: ContactAddressLookupFormProvider,
  mcc: MessagesControllerComponents,
  sessionCache: SessionCache,
  contactAddressLookupView: contact_address_lookup
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {
  private val form = formProvider()

  def onPageLoad(): Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        sessionCache.contactAddressParams.map {
          case Some(contactAddressParams) => Ok(contactAddressLookupView(form.fill(contactAddressParams)))
          case _                          => Ok(contactAddressLookupView(form))
        }
    }

  def submit(): Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction { implicit request => _: LoggedInUserWithEnrolments =>
      form.bindFromRequest().fold(
        formWithError => Future.successful(BadRequest(contactAddressLookupView(formWithError))),
        validAddressParams =>
          sessionCache.saveContactAddressParams(validAddressParams).map { _ =>
            Redirect(
              uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded().url
            )
          }
      )
    }

}
