[![LinkedIn][logo-shield]][logo-url]
[![Issues][issues-shield]][issues-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

<p align="center">
  <h2 align="center">Assignment: Stock Exchange Matching Engine</h2>
   <h3 align="center">Prohibited to Share</h3>
<p>

A Stock Exchange's Matching Engine is fundamental to all trading activities. Not only does it maintains and manages all of the investor’s orders, it also generates trades from them. The Matching Engine has to process a large amount of data at any given interval. On top of this, it has to accomplish multiple functions on the back of each order processed (e.g. sending Market Data Update, Storing the Order, Generating any resulting Trade).

Design a Stock Exchange's Matching Engine’s crossing functionality, storing orders and generate any resulting trades from new orders. Below are some of the requirements:
1.	Implement in Java
2.	Be able to handle multiple client connections into the Engine
3.	Solution needs to be thread safe

You are free to list any assumptions made during this exercise. For example, you may assume that the orders received by the Exchange is of a certain fixed format which you have defined. Bear in mind that the goal of this exercise is to demonstrate your ability to design and implement a workable solution. Avoid 3rd party libraries where possible.

Example:
Say the order book, sorted by price and time looks like this:

```
ID	Side	Time			Qty		Price	Qty		Time				Side
3	    	    	   				20.30	200	162720422493167700		SELL
1	    	    	   				20.30	100	162720422288333000		SELL
2	    	    	   				20.25	100	162720422391511100		SELL
5	BUY	162720422708261600	200		20.20	  	    	    	   	
4	BUY	162720422603839300	100		20.15	  	    	    	   	
6	BUY	162720422819156200	200		20.15	  	    	    	   	
```

NB: The order for sorting by time is ascending for buy-side orders and descending for sell-side orders, so that the order with the highest priority is always in the center and priorities decrease outwards (up or down, depending on the side).

Now imagine a new limit order to "buy 250 shares at 20.35" comes in, then it will be filled, in this order:

100 shares at 20.25 (order #2)
100 shares at 20.30 (order #1)
50 shares at 20.30 (order #3)

This leaves the order book in the following state:

```
ID	Side	Time (nanos)            Qty      Price	  Qty	Time(nanos)		Side
3	    	    	   			 20.30	  150	162720422493167700	SELL
5	BUY	162720422708261600	200	 20.20	  	    	    	   	
4	BUY	162720422603839300	100	 20.15	  	    	    	   	
6	BUY	162720422819156200	200	 20.15	  	    	    	   	
```
Please do not share the question with anyone.

</p>

  <p align="center">
    This is a code implementation for the task within given time frame!
    <br />
    <br />
    <a href="https://github.com/sachinskumar05/SachinBAMLCodeChallenge">View Demo</a>
    ·
    <a href="https://github.com/sachinskumar05/SachinBAMLCodeChallenge/issues">Report Bug</a>
    ·
    <a href="https://github.com/sachinskumar05/SachinBAMLCodeChallenge/issues">Request Feature</a>
  </p>

<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#assumptions">Assumptions</a></li>
        <li><a href="#Future-work-roadmap">Future Work Roadmap</a></li>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgements">Acknowledgements</a></li>
  </ol>
</details>


<!-- ABOUT THE PROJECT -->
## About The Project

This is a sample coding challenge solution for BAML. (Prohibited to Share)

Here's why I implemented this solution:
* Being engaged and Focused on creating something amazing. 
  A project that solves a small problem cna be extended for all 
* Re-Usability. It makes no sense to do same tasks over and over like creating a project from scratch
* Following DRY principles to the rest of my life :smile:

Of course, there is no perfect solution ever existed however, this will serve at-least as a kick-starter 
where future needs may be different. So I'll be adding more in the near future. Thanks for the motivation as a given task

A list of commonly used resources that I find helpful are listed in the acknowledgements.
<!-- built-with -->
### Assumptions
* Client connection will be captured in a Client Adaptor class (In future it will be a FIX/TCP connection)
* Kept only LIMIT Order under current scope, however tried MARKET order as well 
* Only BUY / SELL supported at the moment 

### Future Work Roadmap
* Order Object Pooling (not tested but very close to be completed)
* Gen2 OrderBook backed with BPlusTree partially implemented OrderBookV2 is WIP

### Built With

* [JDK 16](https://www.azul.com/downloads/?package=jdk)
* [BPlusTree](https://creativecommons.org/publicdomain/zero/1.0/) Attempted to use org.ObjectLayout however, it does not sound feasible within given timeframe
* [Spring Boot](https://spring.io/projects/spring-boot) Only Basic structural usages. Can remove this framework without pain


<!-- GETTING STARTED -->
## Getting Started



### Prerequisites
This is an example of how to list things you need to use the software and how to install them.

* [Java Editor](https://www.jetbrains.com/idea/download/#section=windows)
* [JDK 16](https://www.azul.com/downloads/?package=jdk)

### Installation

1.unzip the repo
   ```
   unzip <Application.zip>
   ```
2. import the extracted project in intellij 

<!-- USAGE EXAMPLES -->
## Usage

Run the Main class to start the project
https://www.jetbrains.com/help/idea/run-debug-configuration-spring-boot.html



## Roadmap

See the [open issues](https://github.com/sachinskumar05/SachinBAMLCodeChallenge/issues) for a list of proposed features (and known issues).


##TEST

<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request



<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.



<!-- CONTACT -->
## Contact
Sachin Kumar - [@email]() - sachin.skumar05@gmail.com
Project Link: [https://github.com/your_username/repo_name](https://github.com/sachinskumar05/SachinBAMLCodeChallenge)



<!-- ACKNOWLEDGEMENTS -->
## Acknowledgements
* [Img Shields](https://shields.io)
* [GitHub Pages](https://pages.github.com)
* [Font Awesome](https://fontawesome.com)



[contributors-shield]: images/Contributers.jpg
[contributors-url]: https://github.com/othneildrew/Best-README-Template/graphs/contributors
[linkedin-shield]: images/LinkedIn.jpg
[linkedin-url]: https://www.linkedin.com/in/kumarsac/
[issues-shield]: images/Issues.jpg
[issues-url]: https://github.com/sachinskumar05/SachinBAMLCodeChallenge/issues
[product-screenshot]: images/screenshot.png
[logo-shield]: images/Sachin_Fox_Cafe_Logo_BrandCrowd_Logo_Effect.png
[logo-url]: https://www.linkedin.com/in/kumarsac/
