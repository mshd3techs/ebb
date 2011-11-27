package ebb
package util

import scala.io.Source
import scala.collection.mutable._
import java.security.MessageDigest

/**
 * Read the faq.txt, and get all the questions and answers.
 *
 * Used to generated faq page, or find question and answer by unique key(generated by question).
 */
object FaqReader {

  /**
   * A class represents question, answer, and unique key.
   */
  case class Faq(key: String, question: String, answer: String)

  /**
   * Hold the questions(and answers) by groups.
   */
  private val groups = LinkedHashMap.empty[String, ListBuffer[Faq]]

  // Used to hold the current group when parsing lines
  private var currentGroup = ""

  // Used to hold the current question when parsing lines
  private var currentQuestion = ""

  /**
   * To keep the generated key, avoid duplication.
   */
  private val keys = Set.empty[String]

  // Parsing all lines of faq.txt file, get all questions and answers
  Source.fromURL(this.getClass.getResource("/faq.txt")).getLines.foreach { line =>
    line.trim.toList match {
      // group name starts with "--"
      case '-' :: '-' :: groupName => {
        currentGroup = groupName.mkString
        groups(currentGroup) = ListBuffer()
      }
      // question starts with ": "
      case ':' :: ' ' :: question => currentQuestion = question.mkString
      // answer starts with "> "
      case '>' :: ' ' :: answer => {
        groups(currentGroup) += new Faq(key(currentQuestion), currentQuestion, answer.mkString)
      }
      // ignore empty lines
      case _ =>
    }
  }

  /**
   * Get all group names with original order.
   */
  def getGroupNames: List[String] = groups.keySet.toList

  /**
   * Get guestions of a group.
   */
  def getFaqList(groupName: String): List[Faq] = Nil ++ groups(groupName)

  /**
   * Get the Faq by key.
   */
  def findFaq(key: String): Option[Faq] = {
    for {
      questions <- groups.values
      faq <- questions
    } {
      if (faq.key == key) {
        return Some(faq)
      }
    }
    None
  }

  /**
   * Get the key of a String. Get the md5 hash of it, then find the unique and shortest key more than 5 characters.
   */
  private def key(text: String): String = {
    val dig = MessageDigest.getInstance("MD5")
    val hex = dig.digest("abc".getBytes("UTF8")) map (b => Integer.toHexString(b + 128)) mkString ""
    for (len <- (5 to hex.length)) {
      val key = hex.substring(0, len)
      if (!keys.contains(key)) {
        keys += key
        return key
      }
    }
    return "???"
  }

}