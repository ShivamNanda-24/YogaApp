//
//  ContentView.swift
//  hackathon
//
//  Created by Shivam Nanda on 11/12/22.
//

import SwiftUI

struct ContentView: View {

    var body: some View {
//        let  color1 = UIColor(named: "#51087E") as! Color
    

        ZStack{
            LinearGradient(gradient: Gradient(colors: [.purple, .white, .pink]), startPoint: .topLeading, endPoint: .bottomTrailing)
                .ignoresSafeArea()

            VStack {
                
                ZStack {
                    ForEach(Card.data){card in
                        CardView(card: card)
                    }

                }

                
                    
    

                HStack{
                    HStack{
                        Button(action:{}){
                            Image("dismiss")
                        }
                        Button(action:{}){
                            Image("super_like") 
                        }
                        Button(action:{}){
                            Image("like")
                        }
                        
                    }
                }

            }
            .padding()
            
            
        }

    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}

struct CardView:View{
    @State var card:Card
    var body: some View{
        ZStack(alignment: .leading){

            Image(card.imageName).resizable()
        }

        .cornerRadius(8)
        offset(x: card.x, y: card.y)
        .rotationEffect(.init(degrees: card.degree))
//        .gesture (
//            DragGesture()
//                .onChanged { value in
//                    withAnimation(.default) {
//                        card.x = value.translation.width
//                        // MARK: - BUG 5
//                        card.y = value.translation.height
//                        card.degree = 7 * (value.translation.width > 0 ? 1 : -1)
//                    }
//                }
//            )
        }

    }

